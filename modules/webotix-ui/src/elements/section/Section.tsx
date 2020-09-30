import React from 'react';
import {SectionBox} from "./SectionBox";
import {SectionHeadingBox} from "./SectionHeadingBox";
import {SectionContent, SectionContentScroll} from "./SectionContent";

export interface SectionContext {
    draggable: boolean;
    compactDragHandle: boolean;
}

const Context = React.createContext<SectionContext | null>(null);

export interface SectionProps {
    heading: string;
    nopadding?: boolean;
    scroll?: SectionContentScroll;
}

export class Section extends React.Component<SectionProps> {

    render() {

        const {
            heading,
            children,
            nopadding,
            scroll
        } = this.props;

        return (
            <Context.Consumer>
                {context => (
                    <SectionBox>
                        <SectionHeadingBox className={context &&
                        context.draggable && !context.compactDragHandle ? "dragMe" : undefined}>
                            {heading}
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