import React, {ReactElement} from "react"
import {Breakpoint, breakpoints, cols, DragPanel, KeyedLayouts, OfAllKeyPanel, OfAllPanels, Panel} from "./confg";
import {WidthProvider, Responsive, Layouts, Layout} from "react-grid-layout";
import {DraggableData} from "react-rnd";
import {LayoutBox} from "./elements/layout";
import {Section, SectionProvider} from "./elements/section";
import {ErrorBoundary} from "./components/error";
import {ImmutableObject} from "seamless-immutable";
import {JobsContainer} from "./containers/JobsContainer";
import {Logs} from "./modules/log/Logs";

interface FrameworkProps {

    isMobile: boolean;
    width: number;
    panels: Panel[];
    hiddenPanels: Panel[];
    layouts: ImmutableObject<Layouts>;
    layoutsAsObj: KeyedLayouts;
    showSettings: boolean;

    onToggleViewSettings(): void;

    onTogglePanelAttached(key: OfAllKeyPanel): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onResetLayout(): void;

    onLayoutChange(layout: Layout[], layouts: Layouts): void;

    onMovePanel(key: OfAllKeyPanel, d: DraggableData): void;

    onResizePanel(key: OfAllKeyPanel, d: DragPanel): void;

    onInteractPanel(key: OfAllKeyPanel): void;

    onBreakpointChange(breakpoint: Breakpoint): void
}

interface Renders extends OfAllPanels<() => ReactElement> {
}

const ResponsiveReactGridLayout = WidthProvider(Responsive);

export class Framework extends React.Component<FrameworkProps> {

    panelsRenders: Renders;

    constructor(props: FrameworkProps) {
        super(props);

        const icons = new Map(props.panels.map(panel => [ panel.key, panel.icon ]));

        const Panel: React.FC<{ id: OfAllKeyPanel }> = ({id, children}) => (
            <SectionProvider value={{
                draggable: true,
                icon: icons.get(id),
                compactDragHandle: this.props.isMobile,
                onHide: () => this.props.onTogglePanelVisible(id),
                onToggleAttached: this.props.isMobile ? undefined : () => this.props.onTogglePanelAttached(id)
            }}>
                <ErrorBoundary
                    wrapper={({message, children}) =>
                        <Section heading={message}>{children}</Section>}>
                    {children}
                </ErrorBoundary>
            </SectionProvider>
        );

        this.panelsRenders = {
            jobs: () => (
                <LayoutBox key={"jobs"} data-grid={props.layoutsAsObj.jobs}>
                    <Panel id={"jobs"}>
                        <JobsContainer/>
                    </Panel>
                </LayoutBox>
            ),
            notifications: () => (
                <LayoutBox key={"notifications"} data-grid={props.layoutsAsObj.notifications}>
                    <Panel id={"notifications"}>
                        <Logs/>
                    </Panel>
                </LayoutBox>
            )
        }
    }

    public render() {

        const {
            isMobile,
            panels,
            layouts,
            onLayoutChange,
            onBreakpointChange
        } = this.props;

        return (
            <React.Fragment>
                <ResponsiveReactGridLayout
                    breakpoints={breakpoints}
                    cols={cols}
                    rowHeight={24}
                    layouts={layouts.asMutable({deep: true})}
                    onLayoutChange={onLayoutChange}
                    onBreakpointChange={onBreakpointChange}
                    margin={[4, 4]}
                    containerPadding={[4, 4]}
                    draggableHandle={".dragMe"}>
                    {panels
                        .filter(p => !p.detached || isMobile)
                        .filter(p => p.visible)
                        .map(p => this.panelsRenders[p.key]())
                    }
                </ResponsiveReactGridLayout>

            </React.Fragment>
        )
    }

}

