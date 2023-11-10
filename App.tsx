/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useState } from "react";
import type { PropsWithChildren, ReactNode } from "react";
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text as _Text,
  TextInput,
  useColorScheme,
  View,
  TextProps as _TextProps,
} from "react-native";
import { getGenericPassword, setGenericPassword } from "react-native-keychain";

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from "react-native/Libraries/NewAppScreen";

type SectionProps = {
  title: string;
  children: ReactNode;
};

console.log(Colors);

type TextProps = Omit<_TextProps, "style"> & {
  color?: "default" | "contrast" | "muted" | "primary" | undefined;
  style?: Omit<_TextProps["style"], "color"> | undefined;
};

const COLORS = {
  dark: {
    default: Colors.lighter,
    contrast: Colors.white,
    muted: Colors.light,
    primary: Colors.primary,
  },
  light: {
    default: Colors.darker,
    contrast: Colors.black,
    muted: Colors.dark,
    primary: Colors.primary,
  },
} as const;

function Text({
  color: _color = "default",
  style: _style,
  ...props
}: TextProps): JSX.Element {
  const colorScheme = useColorScheme();
  const color = COLORS[colorScheme ?? "light"][_color];
  const style = [_style, { color }];

  return <_Text style={style} {...props} />;
}

function Section({ children, title }: SectionProps): JSX.Element {
  return (
    <View style={styles.sectionContainer}>
      <Text style={styles.sectionTitle} color="contrast">
        {title}
      </Text>
      <View style={styles.sectionDescription}>{children}</View>
    </View>
  );
}

const USERNAME = "secret" as const;

async function loadSecret(): Promise<string | null> {
  try {
    const result = await getGenericPassword();
    return result === false || result.username !== USERNAME
      ? null
      : result.password;
  } catch (e) {
    console.error(e);
    return null;
  }
}

async function saveSecret(secret: string): Promise<void> {
  await setGenericPassword(USERNAME, secret);
}

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === "dark";

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const [obscured, setObscured] = useState(true);
  const [secret, setSecret] = useState("");
  const [inputKey, setInputKey] = useState(0);

  useEffect(() => {
    let mounted = true;

    loadSecret().then((secret) => {
      if (!mounted || !secret) {
        return;
      }

      console.log("Loaded secret");
      setSecret(secret);
      setInputKey((key) => key + 1);
    });

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? "light-content" : "dark-content"}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}
        contentContainerStyle={{ minHeight: "100%" }}
      >
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
            flexGrow: 1,
          }}
        >
          <Section title="TOTP Settings">
            <Text>Secret Key</Text>
            <TextInput
              key={inputKey}
              autoCapitalize="characters"
              autoCorrect={false}
              blurOnSubmit
              clearButtonMode="while-editing"
              defaultValue={secret}
              enterKeyHint="done"
              multiline={obscured ? undefined : true}
              numberOfLines={obscured ? undefined : 2}
              onEndEditing={(e) => {
                setObscured(true);
                setInputKey((key) => key + 1);

                if (secret !== e.nativeEvent.text) {
                  setSecret(e.nativeEvent.text);
                  saveSecret(e.nativeEvent.text).then(() => {
                    console.log("Updated secret");
                  });
                }
              }}
              onFocus={() => {
                setObscured(false);
              }}
              placeholder="JBSWY3DPEHPK3PXP"
              returnKeyType="done"
              secureTextEntry={obscured}
              style={{
                borderWidth: 1,
                borderColor: isDarkMode ? Colors.light : Colors.dark,
                paddingHorizontal: 8,
                marginVertical: 8,
              }}
              textBreakStrategy="simple"
            />
            <View style={{ flexDirection: "row" }}></View>
          </Section>
          <Section title="Widget Settings">
            <Text>WidgetS settings</Text>
          </Section>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: "600",
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: "400",
  },
  highlight: {
    fontWeight: "700",
  },
});

export default App;
