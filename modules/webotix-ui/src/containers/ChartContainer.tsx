import React, {useContext, useState} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section, SectionTab} from "../elements/section";
import {getValueFromLS} from "../modules/common/localStorage";
import {SocketContext} from "../modules/socket/SocketContext";
import {TradingViewChartContent} from "../components/chart";

interface IntervalProps {
    name: string;
    code: string;
}

const CHART_INTERVAL_KEY = "Chart.interval";

export const ChartContainer: React.FC = () => {

    const socketApi = useContext(SocketContext);

    const visible = useVisibility();

    const selectedCoin = socketApi.selectedCoin;

    const [interval, setInterval] = useState(
        getValueFromLS(CHART_INTERVAL_KEY) || "240"
    );

    const Interval = ({name, code}: IntervalProps) => (
        <SectionTab
            selected={interval === code}
            onClick={() => {
                localStorage.setItem(CHART_INTERVAL_KEY, code);
                setInterval(code);
            }}>
            {name}
        </SectionTab>
    );

    return (
        <RenderIf condition={visible}>
            <Section id={"chart"}
                     heading={"Chart"}
                     buttons={() => (
                         <React.Fragment>
                             <Interval code="W" name="W"/>
                             <Interval code="D" name="D"/>
                             <Interval code="240" name="4H"/>
                             <Interval code="60" name="1H"/>
                             <Interval code="15" name="15m"/>
                         </React.Fragment>
                     )}>
                {selectedCoin ? <TradingViewChartContent
                    coin={selectedCoin} interval={interval}/> : <div>Coin not selected</div>}
            </Section>
        </RenderIf>
    )
};