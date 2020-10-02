import React from 'react';
import {SectionBox} from "./SectionBox";
import {SectionHeadingBox} from "./SectionHeadingBox";
import {SectionContent, SectionContentScroll} from "./SectionContent";
import {Icon} from "../icon";
import {SectionLink} from "./SectionLink";
import {SectionSpan} from "./SectionSpan";
import {SectionIcon} from "./SectionIcon";

export interface SectionContext {
    draggable: boolean;
    compactDragHandle: boolean;
    onHide?: () => void;
    icon?: string;
    onToggleAttached?: () => void;
}

const Context = React.createContext<SectionContext | null>(null);

export interface SectionProps {
    heading: string;
    id?: string;
    nopadding?: boolean;
    scroll?: SectionContentScroll;
    buttons?: () => React.ReactNode;
}

export class Section extends React.Component<SectionProps> {

    render() {

        const {
            heading,
            children,
            nopadding,
            buttons,
            scroll
        } = this.props;

        return (
            <Context.Consumer>
                {context => (
                    <SectionBox>
                        <SectionHeadingBox buttons={buttons} className={context &&
                        context.draggable && !context.compactDragHandle ? "dragMe" : undefined}>
                            {context && !!context.onHide && (
                                <SectionLink onClick={context.onHide}>
                                    <Icon type={"x"}/>
                                </SectionLink>
                            )}
                            {context && context.draggable && context.compactDragHandle && (
                                <SectionSpan>
                                    <Icon type={"move"} className="dragMe" />
                                </SectionSpan>
                            )}
                            {context && context.draggable && !!context.onToggleAttached && (
                                <SectionLink onClick={context.onToggleAttached}>
                                    <Icon type={"external-link"}/>
                                </SectionLink>
                            )}
                            {heading}
                            {context && context.icon && (
                                <SectionIcon>
                                    <Icon type={context.icon}/>
                                </SectionIcon>
                            )}
                        </SectionHeadingBox>
                        <SectionContent nopadding={nopadding} scroll={scroll}>
                            {children}
                        </SectionContent>
                    </SectionBox>
                )}
            </Context.Consumer>
        );
    }
}

export const SectionProvider = Context.Provider;