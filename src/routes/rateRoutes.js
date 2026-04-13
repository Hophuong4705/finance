const router = require('express').Router();
const rateController = require('../controllers/rateController');

// Route lấy tỷ giá (Public)
router.get('/', rateController.getRates);

// Route cập nhật tỷ giá (Thường dùng cho Admin hoặc Tool cập nhật)
router.put('/', rateController.updateRates);

module.exports = router;