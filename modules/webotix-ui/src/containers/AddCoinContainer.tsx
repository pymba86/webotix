import React from "react";
import {RouteComponentProps} from "react-router";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import {Select} from "../elements/select";

interface Person {
    id: number,
    name: string;
}

export const AddCoinContainer: React.FC<RouteComponentProps> = ({history}) => {

    const headerMarkup = (
        <div>Add coin</div>
    );

    const footerMarkup = (
        <Button variant={"primary"}>
            Add
        </Button>
    );

    const options: Person[] = [
        {id: 1, name: "Artem"}
    ];

    const getOptionKey = (person: Person) => `${person.id}`;
    const getOptionLabel = (person: Person) => `${person.name}`;
    const getOptionValue = (person: Person) => person;

    const handleChange = (person: Person) => {
        console.log(person.name);
    }

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
                    <Select placeholder={"Enter name"}
                            options={options}
                            onChange={handleChange}
                            getOptionKey={getOptionKey}
                            getOptionLabel={getOptionLabel}
                            getOptionValue={getOptionValue}
                    />
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