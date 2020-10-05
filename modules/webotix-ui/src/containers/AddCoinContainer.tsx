import React from "react";
import {RouteComponentProps} from "react-router";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Input} from "../elements/input";

export const AddCoinContainer: React.FC<RouteComponentProps> = ({history}) => {

    const headerMarkup = (
        <div>Add coin</div>
    );

    const footerMarkup = (
        <Button variant={"primary"}>
            Add
        </Button>
    );

    return (
        <Modal visible={true}
               closable={true}
               footer={footerMarkup}
               header={headerMarkup}
               onClose={() => history.push("/")}>

            <Form>
                <Form.Item label={"Exchange"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Input placeholder={"Enter name"}/>
                </Form.Item>
                <Form.Item label={"Pair"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Input type={"password"} placeholder={"Enter password"}/>
                </Form.Item>
            </Form>

        </Modal>
    )
}