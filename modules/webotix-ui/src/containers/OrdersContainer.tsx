import React, {useState} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section, SectionTab} from "../elements/section";

enum Mode {
    OPEN = "open",
    HISTORY = "history"
}

export const OrdersContainer: React.FC = () => {

    const visible = useVisibility();

    const [mode, setMode] = useState(Mode.OPEN);

    return (
        <RenderIf condition={visible}>
            <Section id={"orders"}
                     heading={"Orders"}
                     buttons={() => (
                         <React.Fragment>
                             <SectionTab
                                 selected={mode === Mode.OPEN}
                                 onClick={() => setMode(Mode.OPEN)}>
                                 Open
                             </SectionTab>
                             <SectionTab
                                 selected={mode === Mode.HISTORY}
                                 onClick={() => setMode(Mode.HISTORY)}>
                                 History
                             </SectionTab>
                         </React.Fragment>
                     )}>
                orders
            </Section>
        </RenderIf>
    )
}