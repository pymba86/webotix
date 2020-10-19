import React, {useState} from "react";
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
}

export const ScriptEditor: React.FC<ScriptEditorProps> = (
    {
        prefixCls = 'ui-script-editor',
        className
    }) => {

    const [code, setCode] = useState('package ru.webotix.script;\n' +
        '\n' +
        'import io.reactivex.Disposable;\n' +
        '\n' +
        'def start() {\n' +
        '    event.setInterval({event -> {\n' +
        '        notifications.info("Hello world")\n' +
        '    }});\n' +
        '}\n' +
        '\n' +
        'def stop() {\n' +
        '    event.clearInterval()\n' +
        '}');

    const editorRef = useCodeJar({
        code,
        options: {
            tab: ' '.repeat(4)
        },
        onUpdate: setCode,
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