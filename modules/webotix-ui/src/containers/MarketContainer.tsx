import React, {useState} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section, SectionTab} from "../elements/section";
import {getValueFromLS, saveToLS} from "../modules/common/localStorage";
import {MarketTradesContainer} from "./MarketTradesContainer";
import {OrderBookContainer} from "./OrderBookContainer";

enum Mode {
    BOOK = "book",
    HISTORY = "history"
}

const LOCAL_STORAGE_KEY = "MarketContainer.animate"

export const MarketContainer: React.FC = () => {

    const visible = useVisibility();

    const [mode, setMode] = useState(Mode.BOOK);
    const [animate, setAnimate] = useState(getValueFromLS(LOCAL_STORAGE_KEY) !== "false");

    return (
        <RenderIf condition={visible}>
            <Section id={"marketData"}
                     heading={"Market"}
                     nopadding={true}
                     buttons={() => (
                         <React.Fragment>
                             <SectionTab
                                 selected={animate}
                                 onClick={() => {
                                     setAnimate(saveToLS(LOCAL_STORAGE_KEY, !animate));
                                 }}>
                                 Animate
                             </SectionTab>
                             <SectionTab
                                 selected={mode === Mode.BOOK}
                                 onClick={() => setMode(Mode.BOOK)}>
                                 Top Orders
                             </SectionTab>
                             <SectionTab
                                 selected={mode === Mode.HISTORY}
                                 onClick={() => setMode(Mode.HISTORY)}>
                                 Market History
                             </SectionTab>
                         </React.Fragment>
                     )}>
                {mode === Mode.BOOK ? (
                    <OrderBookContainer animate={animate}/>
                ) : (
                    <MarketTradesContainer/>
                )}
            </Section>
        </RenderIf>
    )
}