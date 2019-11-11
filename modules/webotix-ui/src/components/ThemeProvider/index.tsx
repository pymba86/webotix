import * as React from "react";
import defaultTheme, {Theme, ThemeColors} from "../../theme";

const ThemeContext = React.createContext(defaultTheme);

export interface ThemeProviderProps {
    children: React.ReactNode;
    theme?: Theme;
}

export const ThemeProvider = ({
                                  theme = defaultTheme,
                                  children
                              }: ThemeProviderProps) => {
    return (
        <ThemeContext.Provider value={theme}>{children}</ThemeContext.Provider>
    );
};

export function useTheme() {
    return React.useContext(ThemeContext);
}

type RenderCallbackType = (theme: Theme) => React.ReactNode;

export interface ColorModeProps {
    colors: ThemeColors;
    children: RenderCallbackType | React.ReactNode;
    ref: React.Ref<any>;
}

const ColorMode: React.RefForwardingComponent<
    React.Ref<any>,
    ColorModeProps
    > = React.forwardRef(({ colors, children, ...other }: ColorModeProps, ref) => {
    const theme = useTheme();
    const adjustedTheme = React.useMemo(() => mergeColors(theme, colors), [
        theme,
        colors
    ]);
    return (
        <ThemeContext.Provider value={adjustedTheme}>
            {typeof children === "function"
                ? children(adjustedTheme)
                : React.cloneElement(
                    React.Children.only(children) as React.ReactElement<any>,
                    {
                        ref,
                        ...other
                    }
                )}
        </ThemeContext.Provider>
    );
});

ColorMode.displayName = "ColorMode";

function mergeColors(theme: Theme, colors: ThemeColors) {
    return {
        ...theme,
        colors
    };
}

interface ModeProps {
    children: RenderCallbackType | React.ReactNode;
    ref?: any;
}

export const LightMode: React.RefForwardingComponent<
    React.Ref<any>,
    ModeProps
    > = React.forwardRef(({ children, ...other }: ModeProps, ref) => {
    const theme = useTheme();
    return (
        <ColorMode colors={theme.modes.light} ref={ref} {...other}>
            {children}
        </ColorMode>
    );
});

LightMode.displayName = "LightMode";

export const DarkMode: React.RefForwardingComponent<
    React.Ref<any>,
    ModeProps
    > = React.forwardRef(({ children, ...other }: ModeProps, ref) => {
    const theme = useTheme();
    return (
        <ColorMode colors={theme.modes.dark} ref={ref} {...other}>
            {children}
        </ColorMode>
    );
});

DarkMode.displayName = "DarkMode";
