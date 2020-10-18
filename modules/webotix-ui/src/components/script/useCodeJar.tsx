import * as React from "react";
import {CodeJar} from "codejar";

interface Props {
    highlight: (e: HTMLElement) => void;
    options?: Partial<{
        tab: string
        indentOn: RegExp
        spellcheck: boolean
        addClosing: boolean
    }>;
    code: string;
    onUpdate: (code: string) => void;
}

const getCaretOffset = (element: HTMLDivElement) => {
    let caretOffset = 0;
    const doc = element.ownerDocument || (element as any).document;
    const win = doc.defaultView;
    let sel;
    if (win && typeof win.getSelection != "undefined") {
        sel = win.getSelection();
        if (sel && sel.rangeCount > 0) {
            const range = sel.getRangeAt(0);
            const preCaretRange = range.cloneRange();
            preCaretRange.selectNodeContents(element);
            preCaretRange.setEnd(range.endContainer, range.endOffset);
            caretOffset = preCaretRange.toString().length;
        }
    }

    return caretOffset;
};

const createRange = (
    el: ChildNode,
    chars: { count: number },
    range?: Range
): Range => {
    if (!range) {
        range = document.createRange();
        range.selectNode(el);
        range.setStart(el, 0);
    }

    if (chars.count === 0) {
        range.setEnd(el, chars.count);
    } else if (el && chars.count > 0) {
        if (el.nodeType === Node.TEXT_NODE) {
            if (el.textContent!.length < chars.count) {
                chars.count -= el.textContent!.length;
            } else {
                range.setEnd(el, chars.count);
                chars.count = 0;
            }
        } else {
            for (let i = 0; i < el.childNodes.length; i++) {
                range = createRange(el.childNodes[i], chars, range);

                if (chars.count === 0) {
                    break;
                }
            }
        }
    }

    return range;
};

export const setCurrentCursorPosition = (el: HTMLDivElement, chars: any) => {
    if (chars >= 0) {
        const selection = window.getSelection();

        const range = createRange(el, {count: chars});

        if (range) {
            range.collapse(false);
            selection!.removeAllRanges();
            selection!.addRange(range);
        }
    }
};

export const useCodeJar = (props: Props) => {
    const editorRef = React.useRef<HTMLDivElement>(null);
    const jar = React.useRef<CodeJar | null>(null);
    const [cursorOffset, setCursorOffset] = React.useState(0);

    React.useEffect(() => {
        if (!editorRef.current) return;

        jar.current = CodeJar(editorRef.current, props.highlight, props.options);

        jar.current.updateCode(props.code);

        jar.current.onUpdate(txt => {
            if (!editorRef.current) return;

            setCursorOffset(getCaretOffset(editorRef.current));

            props.onUpdate(txt);
        });

        return () => jar.current!.destroy();
    }, []);

    React.useEffect(() => {
        if (!jar.current || !editorRef.current) return;
        jar.current.updateCode(props.code);
        setCurrentCursorPosition(editorRef.current, cursorOffset);
    }, [props.code]);

    React.useEffect(() => {
        if (!jar.current || !props.options) return;

        jar.current.updateOptions(props.options);
    }, [props.options]);

    return editorRef;
};
