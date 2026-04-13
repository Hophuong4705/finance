const router = require('express').Router();
const txController = require('../controllers/transactionController');
const authMW = require('../middleware/auth');

router.get('/', authMW, txController.getTransactions);
router.post('/sync', authMW, txController.syncTransactions);
router.delete('/by-date/:date', authMW, txController.deleteByDate);

module.exports = router;