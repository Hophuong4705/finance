const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
    userId:   { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    amount:   { type: Number, required: true },
    date:     { type: Number, required: true },
    source:   { type: String, default: '' },
    type:     { type: String, enum: ['INCOME', 'EXPENSE'], required: true },
    note:     { type: String, default: '' },
    isSynced: { type: Boolean, default: true }
}, { timestamps: true });

module.exports = mongoose.model('Transaction', transactionSchema);