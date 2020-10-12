import React from "react";
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";
import {Form} from "../../elements/form";
import {Input} from "../../elements/input";

export interface LoginProps {
    error: string;

    onLogin(details: string): void;
}

export const Login: React.FC<LoginProps> = () => {

    const headerMarkup = (
        <div>Login</div>
    );

    const footerMarkup = (
        <div><Button variant={"primary"}>Login</Button></div>
    );

    return (
        <Modal visible={true}
               closable={false}
               footer={footerMarkup}
               header={headerMarkup}>

            <Form>
                <Form.Item label={"Username"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Input placeholder={"Enter name"}/>
                </Form.Item>
                <Form.Item label={"Password"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Input type={"password"} placeholder={"Enter password"}/>
                </Form.Item>
            </Form>

        </Modal>
    )
}