const logger = require('../utils/logger');

let currentRates = { 
    usdToVnd: 25480, 
    eurToVnd: 27650, 
    goldPrice: 120.5 
};

exports.getRates = (req, res) => {
    res.json(currentRates);
};

exports.updateRates = (req, res) => {
    const { usdToVnd, eurToVnd, goldPrice } = req.body;

    if (usdToVnd)  currentRates.usdToVnd  = usdToVnd;
    if (eurToVnd)  currentRates.eurToVnd  = eurToVnd;
    if (goldPrice) currentRates.goldPrice = goldPrice;

    logger.db(`Cập nhật tỷ giá — USD: ${currentRates.usdToVnd} EUR: ${currentRates.eurToVnd} Gold: ${currentRates.goldPrice}`);
    
    res.json({ 
        success: true, 
        rates: currentRates 
    });
};