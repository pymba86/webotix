import  React from 'react'

export const Keys = {
    A: 'KeyA',
    B: 'KeyB',
    C: 'KeyC',
    D: 'KeyD',
    E: 'KeyE',
    F: 'KeyF',
    G: 'KeyG',
    H: 'KeyH',
    I: 'KeyI',
    J: 'KeyJ',
    K: 'KeyK',
    L: 'KeyL',
    M: 'KeyM',
    N: 'KeyN',
    O: 'KeyO',
    P: 'KeyP',
    Q: 'KeyQ',
    R: 'KeyR',
    S: 'KeyS',
    T: 'KeyT',
    U: 'KeyU',
    V: 'KeyV',
    W: 'KeyW',
    X: 'KeyX',
    Y: 'KeyY',
    Z: 'KeyZ',

    '0': 'Digit0',
    '1': 'Digit1',
    '2': 'Digit2',
    '3': 'Digit3',
    '4': 'Digit4',
    '5': 'Digit5',
    '6': 'Digit6',
    '7': 'Digit7',
    '8': 'Digit8',
    '9': 'Digit9',

    F1: 'F1',
    F2: 'F2',
    F3: 'F3',
    F4: 'F4',
    F5: 'F5',
    F6: 'F6',
    F7: 'F7',
    F8: 'F8',
    F9: 'F9',
    F0: 'F0',

    ArrowLeft: 'ArrowLeft',
    ArrowRight: 'ArrowRight',
    ArrowUp: 'ArrowUp',
    ArrowDown: 'ArrowDown',
    Esc: 'Escape',
    Tab: 'Tab',
    Space: 'Space',
    Enter: 'Enter',
    Backspace: 'Backspace',
    CapsLock: 'CapsLock',
    ContextMenu: 'ContextMenu',
    ShiftLeft: 'ShiftLeft',
    ShiftRight: 'ShiftRight',
    ControlLeft: 'ControlLeft',
    ControlRight: 'ControlRight',
    AltLeft: 'AltLeft',
    AltRight: 'AltRight',
    MetaLeft: 'MetaLeft',
    MeatRight: 'MetaRight',

    Num1: 'Numpad1',
    Num2: 'Numpad2',
    Num3: 'Numpad3',
    Num4: 'Numpad4',
    Num5: 'Numpad5',
    Num6: 'Numpad6',
    Num7: 'Numpad7',
    Num8: 'Numpad8',
    Num9: 'Numpad9',
    Num0: 'Numpad0',
    NumLock: 'NumLock',
    NumpadDivide: 'NumpadDivide',
    NumpadMultiply: 'NumpadMultiply',
    NumpadSubtract: 'NumpadSubtract',
    NumpadAdd: 'NumpadAdd',
    NumpadEnter: 'NumpadEnter',
    NumpadDecimal: 'NumpadDecimal',

    Backquote: 'Backquote',
    Backslash: 'Backslash',
    BracketLeft: 'BracketLeft',
    BracketRight: 'BracketRight',
    Comma: 'Comma',
    Delete: 'Delete',
    End: 'End',
    Equal: 'Equal',
    Home: 'Home',
    Insert: 'Insert',
    Minus: 'Minus',
    PageDown: 'PageDown',
    PageUp: 'PageUp',
    Period: 'Period',
    Quote: 'Quote',
    Semicolon: 'Semicolon',
    Slash: 'Slash',
}

export type UseHotkeysProps = {
    active: boolean
    handlers: {
        [keyCode: string]: (event: KeyboardEvent) => void;
    }
}

export const useHotkeys = ({ active, handlers }: UseHotkeysProps) => {
    React.useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.code in handlers) {
                handlers[event.code](event)
            }
        }

        if (active) {
            document.addEventListener('keydown', handleKeyDown)
        }

        return () => {
            document.removeEventListener('keydown', handleKeyDown)
        }
    }, [active, handlers])
}