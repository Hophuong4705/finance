const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    name:      { type: String, required: true },
    email:     { type: String, unique: true, sparse: true, default: null },
    phone:     { type: String, default: '' },
    password:  { type: String, required: true },
    avatarUri: { type: String, default: '' }
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);