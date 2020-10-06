export function getFromLS<T>(key: string): T {
    const result = getValueFromLS(key);
    return result === null ? null : JSON.parse(result);
}

export function getValueFromLS(key: string): string | null {
    try {
        return localStorage.getItem(key) || null;
    } catch (e) {
        return null;
    }
}

export function saveToLS<T>(key: string, value: T): T {
    saveValueToLS(key, JSON.stringify(value));
    return value;
}

export function saveValueToLS(key: string, value: string): void {
    if (localStorage) {
        localStorage.setItem(key, value);
    }
}