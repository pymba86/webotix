import React from "react";
import {useCodeJar} from "./useCodeJar";
import Prism from "prismjs";
import {withLineNumbers} from "codejar/linenumbers";
import "prismjs/components/prism-groovy";

import "prismjs/themes/prism-okaidia.css";
import classNames from "classnames";

const highlight = (editor: HTMLElement) => {
    let code = editor.textContent;
    if (code) {
        editor.innerHTML = Prism.highlight(
            code,
            Prism.languages.groovy,
            "groovy"
        );
    }
};

export interface ScriptEditorProps {
    prefixCls?: string;
    className?: string;
    code?: string;
    onUpdate: (code: string) => void;
}

export const ScriptEditor: React.FC<ScriptEditorProps> = (
    {
        prefixCls = 'ui-script-editor',
        className,
        code = "",
        onUpdate
    }) => {

    const editorRef = useCodeJar({
        code,
        options: {
            tab: ' '.repeat(4)
        },
        onUpdate,
        highlight: withLineNumbers(highlight, {
            class: `${prefixCls}-linenumber`,
            wrapClass: `${prefixCls}-wrap`
        })
    });

    return (
        <div className={classNames(prefixCls, className)}
             ref={editorRef}/>
    );
};