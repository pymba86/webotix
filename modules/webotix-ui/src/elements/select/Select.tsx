import * as React from 'react';
import {useSelect} from "./useSelect";
import {useOutsideClick} from "../../hooks/useOutsideClick";
import {Keys, useHotkeys} from "../../hooks/useHotkeys";
import classNames from 'classnames';
import {Input, InputSize} from "../input";
import {useEffect} from "react";

export interface SelectProps<T> {
    prefixCls?: string;
    options: T[];
    size?: InputSize;
    disabled?: boolean;
    searchable?: boolean;
    noOptionsMessage?: string;
    loadingMessage?: string;
    getOptionKey: (option: T) => string;
    getOptionLabel: (option: T) => string;
    placeholder: string;
    value?: T;
    loading?: boolean;
    prefix?: string;
    optionIsDisabled?: (option: T, idx: number) => boolean;
    onChange: (option: T) => void;
    prefixClassName?: string;
}

export function Select<T>(props: SelectProps<T>) {

    const {
        options = [],
        optionIsDisabled,
        onChange,
        disabled = false,
        loading = false,
        prefixClassName,
        noOptionsMessage = 'No options',
        loadingMessage = 'Loading...',
        prefix,
        prefixCls = 'ui-select',
        placeholder = 'Select...',
        getOptionLabel,
        getOptionKey,
        value,
        searchable,
        size
    } = props;

    const [state, api] = useSelect(options);

    useEffect(() => {
        api.updateOptions(options);
    }, [options]);

    const {
        active,
        menuIsOpen,
        highlightedIdx,
        inputValue,
        visibleOptions
    } = state;

    const inputRef = React.useRef<HTMLInputElement>(null);
    const containerRef = React.useRef<HTMLDivElement>(null);
    const menuRef = React.useRef<HTMLDivElement>(null);

    const css = React.useCallback((className: string) => {
        return `${prefixCls}-${className}`;
    }, [prefixCls]);

    const styles = React.useMemo(() => ({
        active: css('active'),
        container: css('container'),
        currentValue: css('currentValue'),
        disabled: css('disabled'),
        hidden: css('hidden'),
        highlighted: css('highlighted'),
        inputWrapper: css('inputWrapper'),
        label: css('label'),
        labelWrapper: css('labelWrapper'),
        loader: css('loader'),
        loaderItem: css('loaderItem'),
        loaderWrapper: css('loaderWrapper'),
        noOptionsMessage: css('noOptionsMessage'),
        option: css('option'),
        optionsWrapper: css('optionsWrapper'),
        placeholder: css('placeholder'),
        selected: css('selected'),
        icon: css('icon'),
        iconVisible: css('icon-visible'),
    }), [css]);

    const handleInsideClick = (event: Event) => {
        if (disabled) return;

        const refs = menuIsOpen ? [menuRef] : [];

        for (let i = 0; i < refs.length; i += 1) {
            const ref = refs[i].current;
            if (ref && ref.contains(event.target as Node)) {
                return;
            }
        }

        if (menuIsOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    };

    const handOutsideClick = React.useCallback(() => {
        api.blur();
    }, [api, options]);

    const openMenu = React.useCallback(() => {
        api.openMenu();
    }, [api]);

    const closeMenu = React.useCallback(() => {
        api.closeMenu();
        setTimeout(focusInputElement, 0);
    }, [api, options]);

    const blur = React.useCallback(() => {
        api.blur();
        blurInputElement();
    }, [api, options]);

    const focusInputElement = () => {
        if (inputRef?.current) {
            inputRef.current.focus();
        }
    };

    const blurInputElement = () => {
        if (inputRef?.current) {
            inputRef.current.blur();
        }
    };

    const setSelectedOption = React.useCallback(
        (option: T) => {
            onChange(option);
            closeMenu();
        },
        [onChange, closeMenu],
    );

    const selectHighlightedOption = React.useCallback(() => {
        const option = visibleOptions[highlightedIdx];
        if (option) {
            if (optionIsDisabled && optionIsDisabled(option, highlightedIdx)) {
                return;
            }

            setSelectedOption(option);
        }
    }, [optionIsDisabled, setSelectedOption, highlightedIdx, visibleOptions]);

    const scrollItemIntoView = React.useCallback(() => {

        if (menuRef.current) {
            const menu = menuRef.current;

            if (menu) {

                const item: HTMLElement | null = menu.querySelector(
                    `.${styles.highlighted}`);

                if (item) {

                    const isOutOfUpperView = item.offsetTop < menu.scrollTop;
                    const isOutOfLowerView = item.offsetTop + item.clientHeight > menu.scrollTop + menu.clientHeight;

                    if (isOutOfUpperView) {
                        menu.scrollTop = item.offsetTop;
                    } else if (isOutOfLowerView) {
                        menu.scrollTop = item.offsetTop + item.clientHeight - menu.clientHeight;
                    }
                }
            }

        }

    }, [styles]);

    const handleUpKey = React.useCallback(() => {
        if (menuIsOpen) {
            api.setHighlightedOption(-1, true);
            scrollItemIntoView();
        } else {
            openMenu();
        }
    }, [menuIsOpen, api, scrollItemIntoView, openMenu]);

    const handleDownKey = React.useCallback(() => {
        if (menuIsOpen) {
            api.setHighlightedOption(1, true);
            scrollItemIntoView();
        } else {
            openMenu();
        }
    }, [menuIsOpen, api, scrollItemIntoView, openMenu]);

    const handleEnterKey = React.useCallback(() => {
        if (menuIsOpen) {
            selectHighlightedOption();
        } else {
            openMenu();
        }
    }, [selectHighlightedOption, menuIsOpen, openMenu]);

    const handleEscKey = React.useCallback(() => {
        closeMenu();
    }, [closeMenu]);

    const handleTabKey = React.useCallback(
        (event: Event) => {
            if (menuIsOpen && visibleOptions.length) {
                event.preventDefault();
                event.stopPropagation();
                selectHighlightedOption();
            } else {
                blur();
            }
        },
        [blur, selectHighlightedOption, menuIsOpen, visibleOptions],
    );

    const handleOptionMouseDown = (event: React.MouseEvent) => {
        event.preventDefault();
        event.stopPropagation();
    };

    const handleOptionMouseOver = (event: React.MouseEvent, idx: number) => {
        api.setHighlightedOption(idx, false);
    };

    const handleOptionClick = React.useCallback(
        option => {
            onChange(option);
            closeMenu();
        },
        [closeMenu, onChange],
    );

    React.useEffect(() => {
        if (active) {
            setTimeout(focusInputElement, 0)
        }
    }, [active]);


    useOutsideClick({
        containerRef,
        onInsideClick: handleInsideClick,
        onOutsideClick: handOutsideClick,
    });

    useHotkeys({
        active: active,
        handlers: {
            [Keys.ArrowUp]: handleUpKey,
            [Keys.ArrowDown]: handleDownKey,
            [Keys.Enter]: handleEnterKey,
            [Keys.Esc]: handleEscKey,
            [Keys.Tab]: handleTabKey,
        },
    });

    return (
        <div className={classNames(styles.container, {
            [styles.disabled]: disabled,
        })}
             ref={containerRef}>
            <div
                className={classNames(styles.inputWrapper, {
                    [styles.active]: menuIsOpen,
                    [styles.disabled]: disabled,
                })}>

                <Input
                    value={inputValue}
                    disabled={disabled}
                    size={size}
                    onChange={(value => api.inputChange(value, options, getOptionLabel))}
                    onFocus={api.focus}
                    readOnly={!searchable}
                    prefix={prefix}
                    type={"text"}
                    prefixClassName={prefixClassName}
                    suffix={loading ? "loader" : "chevron-down"}
                    wrapperRef={inputRef}
                    suffixClassName={classNames(styles.icon, {
                        [styles.iconVisible]: !loading && menuIsOpen
                    })}
                />

                <div
                    className={classNames(styles.currentValue, {
                        [styles.hidden]: !!inputValue,
                        [styles.placeholder]: !value,
                    })}

                >
                    {(value && getOptionLabel(value)) || placeholder}
                </div>


            </div>

            {menuIsOpen && (
                <div className={classNames(styles.optionsWrapper)}
                     ref={menuRef}>
                    {visibleOptions.length ? (
                        visibleOptions.map((option, idx) => {

                            const key = getOptionKey ? getOptionKey(option) : idx;

                            const isHighlighted = idx === highlightedIdx;

                            const isSelected = value
                                && (getOptionKey(option) === getOptionKey(value));

                            const isDisabled = optionIsDisabled
                                ? optionIsDisabled(option, idx)
                                : false;

                            return (
                                <div tabIndex={idx}
                                     className={classNames(styles.option, {
                                         [styles.highlighted]: isHighlighted,
                                         [styles.selected]: isSelected,
                                         [styles.disabled]: isDisabled,
                                     })}
                                     key={key}
                                     onClick={
                                         isDisabled ? undefined : () => handleOptionClick(option)
                                     }
                                     onMouseDown={isDisabled ? undefined : handleOptionMouseDown}
                                     onMouseOver={
                                         isDisabled
                                             ? undefined
                                             : event => handleOptionMouseOver(event, idx)
                                     }>
                                    {getOptionLabel(option)}
                                </div>
                            )
                        })
                    ) : (
                        <div className={classNames(styles.option, styles.noOptionsMessage)}>
                            {loading ? loadingMessage : noOptionsMessage}
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}
