import React, {useState} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section, SectionTab} from "../elements/section";
import {TradeHistoryContainer} from "./TradeHistoryContainer";
import {OpenOrdersContainer} from "./OpenOrdersContainer";

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
                     nopadding={true}
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
                {mode === Mode.OPEN ? (
                    <OpenOrdersContainer/>
                ) : (
                    <TradeHistoryContainer/>
                )}
            </Section>
        </RenderIf>
    )
};
