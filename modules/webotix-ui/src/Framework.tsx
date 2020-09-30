import React, {ReactElement} from "react"
import {DragPanel, OfAllPanels, Panel} from "./confg";
import {WidthProvider, Responsive, Layouts, Layout} from "react-grid-layout";
import {Rnd, DraggableData} from "react-rnd";
import {LayoutBox} from "./elements/layout";
import {Section, SectionProvider} from "./elements/section";
import {ErrorBoundary} from "./components/error";

interface FrameworkProps {

    isMobile: boolean;
    width: number;
    panels: Panel[];
    hiddenPanels: Panel[];
    layouts: Layouts;
    showSettings: boolean;

    onToggleViewSettings(): void;

    onTogglePanelAttached(key: string): void;

    onTogglePanelVisible(key: string): void;

    onResetLayout(): void;

    onLayoutChange(layout: Layout[], layouts: Layouts): void;

    onMovePanel(key: string, d: DraggableData): void;

    onResizePanel(Key: string, d: DragPanel): void;

    onBreakpointChange(breakpoint: string): void;
}

interface Renders extends OfAllPanels<() => ReactElement> {
}

export class Framework extends React.Component<FrameworkProps> {

    panelsRenders: Renders;

    constructor(props: FrameworkProps) {
        super(props);

        const Panel: React.FC<{ id: string }> = ({id, children}) => (
            <SectionProvider value={{
                draggable: true,
                compactDragHandle: this.props.isMobile
            }}>
                <ErrorBoundary wrapper={({message, children}) =>
                    <Section heading={message}>{children}</Section>}>
                    {children}
                </ErrorBoundary>
            </SectionProvider>
        );


        this.panelsRenders = {
            jobs: () => (
                <LayoutBox key={"jobs"}>
                    <Panel id={"jobs"}>
                        123
                    </Panel>
                </LayoutBox>
            )
        }
    }

    public render() {

        const {
            isMobile,
            width,
            panels,
            hiddenPanels,
            layouts,
            showSettings,
            onLayoutChange,
            onMovePanel,
            onResetLayout,
            onResizePanel,
            onTogglePanelAttached,
            onTogglePanelVisible,
            onToggleViewSettings
        } = this.props;

        return (<div>13413</div>)
    }

}

