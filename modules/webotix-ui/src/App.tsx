import React from 'react';
import {Button} from 'elements/button';
import {Modal} from "./elements/modal";
import {Input} from "./elements/input";
import {Form} from "./elements/form";

function App() {
    return (
        <div className="App">
            <Modal visible={true} closable={false}
                   footer={(<div><Button variant={"primary"}>Login</Button></div>)}
                   header={(<div>Login</div>)}>

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
            <header className="App-header">

            </header>
        </div>
    );
}

export default App;
