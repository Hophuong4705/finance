const chalk = require('chalk');

const timestamp = () => {
    const now = new Date();
    return now.toLocaleString('vi-VN', { 
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit' 
    });
};

const print = (color, icon, msg) => {
    const time = `[${timestamp()}]`;
    process.stdout.write(chalk[color](`${time} ${icon} `));
    console.log(msg);
};

const logger = {
    info:    (msg) => print('cyan',    'ℹ️ ', msg),
    success: (msg) => print('green',   '✅', msg),
    warn:    (msg) => print('yellow',  '⚠️ ', msg),
    error:   (msg) => print('red',     '❌', msg),
    db:      (msg) => print('magenta', '🗄️ ', msg),
    api:     (msg) => print('blue',    '🌐', msg),
    sync:    (msg) => print('yellow',  '🔄', msg),
};

module.exports = logger;