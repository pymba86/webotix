import {Reducer, useMemo, useReducer} from "react";

export interface SelectState<T> {
    active: boolean;
    highlightedIdx: number;
    inputValue: string;
    menuIsOpen: boolean;
    visibleOptions: T[];
}

export interface SelectApi<T> {

    openMenu(): void;

    closeMenu(options: T[]): void;

    blur(options: T[]): void;

    focus(): void;

    setHighlightedOption(highlightIdx: number, relative: boolean): void;

    inputChange(inputValue: string, options: T[], getOptionLabel: (option: T) => string): void;
}

interface BaseAction<T> {
    reduce(state: SelectState<T>): SelectState<T>;
}

function reducer<T>(state: SelectState<T>, action: BaseAction<T>): SelectState<T> {
    return action.reduce(state);
}

class UpdateStateAction<T> implements BaseAction<T> {

    private readonly upState: Partial<SelectState<T>>;

    constructor(upState: Partial<SelectState<T>>) {
        this.upState = upState;
    }

    reduce(state: SelectState<T>): SelectState<T> {
        return {
            ...state,
            ...this.upState
        };
    }
}

class InputChangeAction<T> implements BaseAction<T> {

    private readonly inputValue: string;
    private readonly getOptionLabel: (option: T) => string;
    private readonly options: T[];


    constructor(inputValue: string, options: T[], getOptionLabel: (option: T) => string) {
        this.inputValue = inputValue;
        this.getOptionLabel = getOptionLabel;
        this.options = options;
    }

    reduce(state: SelectState<T>): SelectState<T> {

        const options = this.filterOptionsBySearch(this.options);

        return {
            ...state,
            highlightedIdx: 0,
            inputValue: this.inputValue,
            menuIsOpen: true,
            visibleOptions: options
        };
    }

    filterOptionsBySearch(options: T[]): T[] {

        const filterOptions = (option: T) =>
            this.getOptionLabel(option)
                .toLowerCase()
                .includes(this.inputValue.toLowerCase())

        return options.filter(filterOptions)
    }

}

class UpdateHighlightIndex<T> implements BaseAction<T> {

    private readonly idx: number;
    private readonly relative: boolean;

    constructor(idx: number, relative: boolean) {
        this.idx = idx;
        this.relative = relative;
    }

    reduce(state: SelectState<T>): SelectState<T> {

        const newIdx = this.relative
            ? state.highlightedIdx + this.idx : this.idx;

        const highlightedIdx = this.wrap(
            0, state.visibleOptions.length - 1, newIdx);

        if (highlightedIdx === state.highlightedIdx) {
            return state;
        } else {
            return {
                ...state,
                highlightedIdx: this.idx
            };
        }
    }

    wrap(min: number, max: number, val: number): number {
        if (val < min) return max;
        if (val > max) return min;
        return val;
    }
}

export function useSelect<T>(options: T[]): [SelectState<T>, SelectApi<T>] {

    const [value, dispatch] = useReducer<Reducer<SelectState<T>, BaseAction<T>>>(reducer,
        {
            active: false,
            highlightedIdx: 0,
            inputValue: '',
            menuIsOpen: false,
            visibleOptions: options
        });

    const api: SelectApi<T> = useMemo(() => ({
        inputChange(inputValue: string, options: T[], getOptionLabel: (option: T) => string): void {
            dispatch(new InputChangeAction(inputValue, options, getOptionLabel));
        },
        openMenu() {
            dispatch(new UpdateStateAction(
                {
                    active: true,
                    highlightedIdx: 0,
                    menuIsOpen: true
                })
            )
        },
        blur(options: T[]) {
            dispatch(new UpdateStateAction(
                {
                    active: false,
                    highlightedIdx: 0,
                    inputValue: '',
                    menuIsOpen: false,
                    visibleOptions: options
                })
            )
        },
        closeMenu(options: T[]) {
            dispatch(new UpdateStateAction(
                {
                    highlightedIdx: 0,
                    inputValue: '',
                    menuIsOpen: false,
                    visibleOptions: options
                })
            )
        },
        focus() {
            dispatch(new UpdateStateAction(
                {
                    active: true
                })
            )
        },
        setHighlightedOption(highlightIdx: number, relative: boolean) {
            dispatch(new UpdateHighlightIndex(highlightIdx, relative));
        }
    }), [dispatch]);

    return [value, api];
}