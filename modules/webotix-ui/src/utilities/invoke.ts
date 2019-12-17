
export function isFunction(value: any): value is Function {
    return typeof value === "function";
}

/**
 * Safely invoke the function with the given arguments, if it is indeed a
 * function, and return its value. Otherwise, return undefined.
 */
export function safeInvoke<R>(func: (() => R) | undefined): R | undefined;
export function safeInvoke<A, R>(func: ((arg1: A) => R) | undefined, arg1: A): R | undefined;
export function safeInvoke<A, B, R>(func: ((arg1: A, arg2: B) => R) | undefined, arg1: A, arg2: B): R | undefined;
export function safeInvoke<A, B, C, R>(
    func: ((arg1: A, arg2: B, arg3: C) => R) | undefined,
    arg1: A,
    arg2: B,
    arg3: C,
): R | undefined;
export function safeInvoke<A, B, C, D, R>(
    func: ((arg1: A, arg2: B, arg3: C, arg4: D) => R) | undefined,
    arg1: A,
    arg2: B,
    arg3: C,
    arg4: D,
): R | undefined;
// tslint:disable-next-line:ban-types
export function safeInvoke(func: Function | undefined, ...args: any[]) {
    if (isFunction(func)) {
        return func(...args);
    }
    return undefined;
}

/**
 * Safely invoke the provided entity if it is a function; otherwise, return the
 * entity itself.
 */
export function safeInvokeOrValue<R>(funcOrValue: (() => R) | R | undefined): R;
export function safeInvokeOrValue<A, R>(funcOrValue: ((arg1: A) => R) | R | undefined, arg1: A): R;
export function safeInvokeOrValue<A, B, R>(funcOrValue: ((arg1: A, arg2: B) => R) | R | undefined, arg1: A, arg2: B): R;
export function safeInvokeOrValue<A, B, C, R>(
    funcOrValue: ((arg1: A, arg2: B, arg3: C) => R) | R | undefined,
    arg1: A,
    arg2: B,
    arg3: C,
): R;
export function safeInvokeOrValue<A, B, C, D, R>(
    funcOrValue: ((arg1: A, arg2: B, arg3: C, arg4: D) => R) | R | undefined,
    arg1: A,
    arg2: B,
    arg3: C,
    arg4: D,
): R;
// tslint:disable-next-line:ban-types
export function safeInvokeOrValue(funcOrValue: Function | any | undefined, ...args: any[]) {
    return isFunction(funcOrValue) ? funcOrValue(...args) : funcOrValue;
}
