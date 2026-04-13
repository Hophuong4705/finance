const Transaction = require('../models/Transaction');
const User = require('../models/User');
const logger = require('../utils/logger');

exports.getTransactions = async (req, res) => {
    try {
        const txs = await Transaction.find({ userId: req.user.id }).sort({ date: -1 });
        res.json(txs);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.syncTransactions = async (req, res) => {
    const transactions = req.body;
    if (!Array.isArray(transactions)) return res.status(400).json({ error: 'Data must be array' });

    try {
        const userExists = await User.findById(req.user.id);
        
        if (!userExists) {
            logger.error(`⚠️ CẢNH BÁO: Phát hiện "User Ma" đang cố đồng bộ dữ liệu!`);
            logger.warn(`User ID: ${req.user.id} đã bị xóa khỏi hệ thống Mongoose.`);
            
            return res.status(401).json({ 
                success: false, 
                message: "ACCOUNT_DELETED",
                error: "Tài khoản này không còn tồn tại trên hệ thống!" 
            });
        }

        logger.sync(`Sync GD cho ${req.user.name}: ${transactions.length} bản ghi`);

        const ops = transactions.map(tx => ({
            updateOne: {
                filter: { userId: req.user.id, date: tx.date }, 
                update: { $set: { ...tx, userId: req.user.id, isSynced: true } },
                upsert: true 
            }
        }));

        if (ops.length > 0) await Transaction.bulkWrite(ops);
        
        logger.success(`Sync hoàn tất cho ${req.user.name}`);
        res.json({ success: true });
    } catch (err) {
        logger.error(`Lỗi Sync: ${err.message}`);
        res.status(500).json({ error: err.message });
    }
};

exports.deleteByDate = async (req, res) => {
    try {
        // 1. Kiểm tra User Ma y như hàm Sync để bảo mật tuyệt đối
        const userExists = await User.findById(req.user.id);
        
        if (!userExists) {
            logger.warn(`⚠️ CẢNH BÁO: Phát hiện "User Ma" đang cố xóa dữ liệu!`);
            return res.status(401).json({ 
                success: false, 
                message: "ACCOUNT_DELETED",
                error: "Tài khoản không tồn tại!" 
            });
        }

        // 2. 🔥 QUAN TRỌNG: Ép kiểu String từ URL thành Number (Long) để MongoDB hiểu đúng
        const dateMs = Number(req.params.date);

        // 3. Thực hiện xóa
        const deletedTx = await Transaction.findOneAndDelete({ 
            userId: req.user.id, 
            date: dateMs 
        });

        if (deletedTx) {
            logger.success(`🗑️ Đã xóa giao dịch lúc ${dateMs} của User: ${req.user.id}`);
        } else {
            logger.warn(`⚠️ Không tìm thấy giao dịch để xóa (Date: ${dateMs})`);
        }

        res.json({ success: true });
    } catch (err) {
        logger.error(`Lỗi Xóa GD: ${err.message}`);
        res.status(500).json({ error: err.message });
    }
};