const Notification = require('../models/Notification');
const logger       = require('../utils/logger');

// [GET] Lấy danh sách
exports.getNotifications = async (req, res) => {
    try {
        const notifs = await Notification.find({ userId: req.user.id }).sort({ createdAt: -1 });
        res.json(notifs);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// [POST] Đồng bộ (Xóa cũ - Nạp mới)
exports.syncNotifications = async (req, res) => {
    const items = req.body;
    try {
        logger.sync(`Đang đồng bộ lại thông báo cho ${req.user.name}...`);
        await Notification.deleteMany({ userId: req.user.id });
        if (items && items.length > 0) {
            const docs = items.map(n => ({ ...n, userId: req.user.id }));
            await Notification.insertMany(docs);
        }
        logger.success(`Đã cập nhật danh sách thông báo mới cho ${req.user.name}`);
        res.json({ success: true });
    } catch (err) {
        logger.error(`Lỗi Sync thông báo: ${err.message}`);
        res.status(500).json({ error: err.message });
    }
};

// 🔥 HÀM MỚI: Xóa 1 cái duy nhất theo ID của MongoDB
exports.deleteNotificationById = async (req, res) => {
    try {
        const result = await Notification.findOneAndDelete({ 
            _id: req.params.id, 
            userId: req.user.id 
        });
        if (result) {
            logger.db(`Đã xóa 1 thông báo — User: ${req.user.name}`);
            res.json({ success: true });
        } else {
            res.status(404).json({ error: "Không tìm thấy thông báo để xóa" });
        }
    } catch (err) {
        logger.error(`Lỗi xóa thông báo: ${err.message}`);
        res.status(500).json({ error: err.message });
    }
};

// [DELETE] Xóa sạch bách
exports.deleteAllNotifications = async (req, res) => {
    try {
        await Notification.deleteMany({ userId: req.user.id });
        logger.db(`Đã dọn sạch thông báo — User: ${req.user.name}`);
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};