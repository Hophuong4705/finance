const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const logger = require('../utils/logger');

exports.register = async (req, res) => {
    try {
        const { name, email, phone, password } = req.body;
        if (!name || !password) return res.status(400).json({ error: 'Thiếu thông tin' });

        const hash = await bcrypt.hash(password, 10);
        const user = await User.create({ name, email, phone, password: hash });

        logger.success(`Đăng ký: ${name} (${email})`);

        const token = jwt.sign({ id: user._id, name: user.name }, process.env.JWT_SECRET, { expiresIn: '30d' });
        res.json({ success: true, token, userId: user._id, name: user.name });
    } catch (err) {
        if (err.code === 11000) return res.status(409).json({ error: 'Email đã tồn tại' });
        res.status(500).json({ error: err.message });
    }
};

exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });
        if (!user || !(await bcrypt.compare(password, user.password))) {
            return res.status(401).json({ error: 'Sai tài khoản hoặc mật khẩu' });
        }

        logger.success(`Đăng nhập: ${user.name}`);
        const token = jwt.sign({ id: user._id, name: user.name }, process.env.JWT_SECRET, { expiresIn: '30d' });
        res.json({ success: true, token, userId: user._id, name: user.name });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.changePassword = async (req, res) => {
    try {
        const { oldPassword, newPassword } = req.body;
        
        const user = await User.findById(req.user.id);
        if (!user) return res.status(401).json({ success: false, message: "ACCOUNT_DELETED" });

        const isMatch = await bcrypt.compare(oldPassword, user.password);
        if (!isMatch) return res.status(400).json({ success: false, message: "Sai mật khẩu hiện tại" });

        const salt = await bcrypt.genSalt(10);
        user.password = await bcrypt.hash(newPassword, salt);
        await user.save();

        logger.success(`Đổi mật khẩu: ${user.name}`);
        res.json({ success: true, message: "Đổi mật khẩu thành công" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};