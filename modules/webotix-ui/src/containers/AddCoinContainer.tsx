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

    const [value, setValue] = React.useState<Person | undefined>(undefined);

    const headerMarkup = (
        <div>Add coin</div>
    );

    const footerMarkup = (
        <Button variant={"primary"}>
            Add
        </Button>
    );

    const options: Person[] = [
        {id: 1, name: "Binance"},
        {id: 2, name: "Yobit"},
        {id: 3, name: "B"},
        {id: 4, name: "C"},
        {id: 5, name: "D"},
        {id: 6, name: "E"},
        {id: 7, name: "H"},
        {id: 8, name: "D"},
        {id: 9, name: "A"},
        {id: 10, name: "V"},
    ];

    const getOptionKey = (person: Person) => `${person.id}`;
    const getOptionLabel = (person: Person) => `${person.name}`;

    const handleChange = (person: Person) => {
        setValue(person);
    };

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
                            value={value}
                            options={options}
                            onChange={handleChange}
                            getOptionKey={getOptionKey}
                            getOptionLabel={getOptionLabel}
                    />
                </Form.Item>
                <Form.Item label={"Pair"} required={true}
                           message={"Please input your name"} invalid={false}
                           status={"error"}>
                    <Select placeholder={"Enter name"}
                            value={value}
                            options={options}
                            onChange={handleChange}
                            getOptionKey={getOptionKey}
                            getOptionLabel={getOptionLabel}
                    />
                </Form.Item>
            </Form>

        </Modal>
    )
}