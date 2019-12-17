// Получить параметры окружения
export function getEnvParams() {
    const isProduction = process.env.NODE_ENV === 'production';
    const isDevelopment = process.env.NODE_ENV === 'development';
    const isWatchMode = process.env.WATCH_MODE === 'true';

    const chunkName = isProduction ? 'id' : 'name';
    const chunkHash = isWatchMode && !isProduction ? 'hash' : 'chunkhash';
    const withHot = isWatchMode && isDevelopment;

    return {
        isProduction,
        isDevelopment,
        isWatchMode,
        chunkName,
        chunkHash,
        withHot
    };
}