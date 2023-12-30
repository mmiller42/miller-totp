import React, { useState } from "react";
import { TextInputProps } from "react-native";
import { TextInput } from "./TextInput";

export type SecretInputProps = Omit<
  TextInputProps,
  | "autoCorrect"
  | "blurOnSubmit"
  | "defaultValue"
  | "enterKeyHint"
  | "multiline"
  | "numberOfLines"
  | "returnKeyType"
  | "secureTextEntry"
  | "textBreakStrategy"
  | "value"
> & {
  value: string;
  onSubmitValue: (value: string) => void;
};

export function SecretInput({
  value: defaultValue,
  onSubmitValue,
  onEndEditing,
  onFocus,
  ...props
}: SecretInputProps): JSX.Element {
  const [obscured, setObscured] = useState(true);
  const [bit, setBit] = useState(0);
  const key = `${bit}-${defaultValue}`;

  const obscuredProps = obscured
    ? { secureTextEntry: true }
    : { multiline: true, numberOfLines: 2 };
  return (
    <TextInput
      key={key}
      autoCorrect={false}
      blurOnSubmit
      defaultValue={defaultValue}
      enterKeyHint="done"
      onEndEditing={(event) => {
        setObscured(true);
        setBit((bit) => (bit ? 0 : 1));

        const value = event.nativeEvent.text.trim().toUpperCase();
        onSubmitValue(value);
        onEndEditing?.(event);
      }}
      onFocus={(event) => {
        setObscured(false);
        onFocus?.(event);
      }}
      returnKeyType="done"
      textBreakStrategy="simple"
      {...props}
      {...obscuredProps}
    />
  );
}
