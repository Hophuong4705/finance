const jwt = require('jsonwebtoken');
const logger = require('../utils/logger');

module.exports = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        logger.warn(`Truy cập bị chặn — Thiếu Token — ${req.method} ${req.path}`);
        return res.status(401).json({ error: 'Bạn cần đăng nhập để thực hiện thao tác này' });
    }

    const token = authHeader.split(' ')[1];

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        logger.error(`Token không hợp lệ (${err.message}) — ${req.method} ${req.path}`);
        res.status(401).json({ error: 'Phiên đăng nhập hết hạn hoặc không hợp lệ' });
    }
};