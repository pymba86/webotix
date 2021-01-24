import React, {FormEvent, useState} from "react";
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";
import {Form} from "../../elements/form";
import {Input} from "../../elements/input";
import LoginDetails from "./LoginDetails";

export interface LoginProps {
    error?: string;

    onLogin(details: LoginDetails): void
}

export const Login: React.FC<LoginProps> = (props: LoginProps) => {

    const [username, setUserName] = useState("")
    const [password, setPassword] = useState("")

    const headerMarkup = (
        <div>Login</div>
    );

    const footerMarkup = (
        <div>
            <Button variant={"primary"}
                    onClick={() => props.onLogin({username, password})}>
                Login
            </Button>
        </div>
    );

    return (
        <Modal visible={true}
               closable={false}
               footer={footerMarkup}
               header={headerMarkup}>

            <Form>
                <Form.Item label={"Username"} required={true}
                           message={"Please input your name"}
                           invalid={false}
                           status={"default"}>
                    <Input
                        placeholder={"Enter name"}
                        value={username}
                        onChange={setUserName}
                    />
                </Form.Item>
                <Form.Item label={"Password"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Input type={"password"}
                           placeholder={"Enter password"}
                           value={password}
                           onChange={setPassword}/>
                </Form.Item>
            </Form>

        </Modal>
    )
}
