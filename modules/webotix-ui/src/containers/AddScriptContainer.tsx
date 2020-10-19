import React, {useContext, useEffect} from "react";
import {RouteComponentProps} from "react-router";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Select} from "../elements/select";
import {MarketContext} from "../modules/market/MarketContext";
import {Coin, Exchange, PartialServerCoin} from "../modules/market";
import {LogContext} from "../modules/log/LogContext";
import exchangeService from "../modules/market/exchangeService";
import {augmentCoin} from "../modules/market/utils";
import {ServerContext} from "../modules/server/ServerContext";
import {Input} from "../elements/input";

export const AddScriptContainer: React.FC<RouteComponentProps> = ({history}) => {


    const footerMarkup = (
        <Button variant={"primary"}>
            Add
        </Button>
    );

    return (
        <Modal visible={true} closable={true}
               footer={footerMarkup}
               header={"Add coin"}
               onClose={() => history.push("/")}>

            <Form>
                <Form.Item label={"Script"} required={true}>
                    <Input placeholder={"Enter name"}/>
                </Form.Item>
            </Form>
        </Modal>
    )
};