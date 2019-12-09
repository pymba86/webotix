
import {WidthProvider, Responsive} from "react-grid-layout"
import * as React from "react";
import {Button} from "../../elements/Button";

const ResponsiveReactGridLayout = WidthProvider(Responsive);

interface Props {
    isMobile: boolean;
}


export default class Framework extends React.Component<Props> {

    basePanels: any = {
        coins: {
            key: "coins",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        jobs: {
            key: "jobs",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        chart: {
            key: "chart",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        openOrders: {
            key: "openOrders",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        balance: {
            key: "balance",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        tradeSelector: {
            key: "tradeSelector",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        marketData: {
            key: "marketData",
            visible: true,
            detached: false,
            stackPosition: 0
        },
        notifications: {
            key: "notifications",
            visible: true,
            detached: false,
            stackPosition: 0
        }
    };

    baseLayouts: any = {
        lg: [
          { i: "coins", x: 0, y: 0, w: 8, h: 22 },
            { i: "notifications", x: 0, y: 100, w: 8, h: 11 }
        ],
        md: [
            { i: "coins", x: 20, y: 100, w: 12, h: 11 },
            { i: "notifications",x: 20, y: 400, w: 12, h: 7 }
        ],
        sm: [
            { i: "coins", x: 0, y: 100, w: 4, h: 12},
            { i: "notifications", x: 0, y: 800, w: 4, h: 6 }
        ]
    };

    render() {
        return (
            <ResponsiveReactGridLayout
                layouts={this.baseLayouts}
            >
                  <div  key="coins">
                      <Button primary={true}>Добавить заказ</Button>
                  </div>
                    <div key="notifications">
                        <Button>Загрузить</Button>
                    </div>
            </ResponsiveReactGridLayout>
        )
    }
}
