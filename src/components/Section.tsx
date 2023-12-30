import React, { ReactNode } from "react";
import { StyleSheet, View } from "react-native";
import { Text } from "./Text";

export type SectionProps = {
  title: string;
  children: ReactNode;
};

export function Section({ children, title }: SectionProps): JSX.Element {
  return (
    <View style={styles.root}>
      <Text size={4 / 3} color="contrast">
        {title}
      </Text>
      <View style={styles.body}>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  body: {
    marginTop: 8,
  },
});
