const router   = require('express').Router();
const authMW   = require('../middleware/auth');
const notifController = require('../controllers/notificationController');

router.use(authMW);

router.get('/', notifController.getNotifications);
router.post('/sync', notifController.syncNotifications);

// 🔥 Route xóa 1 cái (Ví dụ: DELETE /api/notifications/65f1abc...)
router.delete('/:id', notifController.deleteNotificationById); 

// Route xóa tất cả (DELETE /api/notifications)
router.delete('/', notifController.deleteAllNotifications);

module.exports = router;