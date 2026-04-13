const express  = require('express');
const mongoose = require('mongoose');
const cors     = require('cors');
const logger   = require('./utils/logger'); 
require('dotenv').config();

const app = express();

app.use(cors());
app.use(express.json());

app.use((req, _res, next) => {
    next(); 
});

app.use('/api/auth',          require('./routes/authRoutes'));
app.use('/api/transactions',  require('./routes/transactionRoutes'));
app.use('/api/notifications', require('./routes/notificationRoutes'));
app.use('/api/rates',         require('./routes/rateRoutes'));
app.get('/api/health', (_, res) => res.json({ status: 'OK' }));

app.use((req, res) => {
    logger.warn(`404 — ${req.method} ${req.originalUrl}`);
    res.status(404).json({ error: 'Route not found' });
});

const PORT = process.env.PORT || 3000;

mongoose.connect(process.env.MONGO_URI)
    .then(() => {
        app.listen(PORT, '0.0.0.0', () => {
            console.clear();
            logger.success(`✅ Backend ON - Port: ${PORT}`);
            logger.info('ℹ️  🚀 Đang chờ hành động từ App UED...');
        });
    })
    .catch(err => {
        logger.error(`❌ MongoDB connection failed: ${err.message}`);
        process.exit(1);
    });