/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useRef, useState } from "react";
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  TextInput as RNTextInput,
  useColorScheme,
  View,
} from "react-native";
import {
  getGenericPassword,
  resetGenericPassword,
  setGenericPassword,
} from "react-native-keychain";

import { Colors, Header } from "react-native/Libraries/NewAppScreen";
import { Text } from "./src/components/Text";
import { SecretInput } from "./src/components/SecretInput";
import { Section } from "./src/components/Section";
import { Stack } from "./src/components/Stack";
import { TextInput } from "./src/components/TextInput";

const USERNAME = "secret" as const;

type AppData = {
  secret: string;
  digits: number;
  period: number;
};

const DEFAULT_DATA: AppData = {
  secret: "JBSWY3DPEHPK3PXP",
  digits: 6,
  period: 30,
};

async function loadData(): Promise<AppData> {
  try {
    const result = await getGenericPassword();
    console.log("got data:", result);

    if (!result || result.username !== USERNAME) {
      await saveData(DEFAULT_DATA);
      return DEFAULT_DATA;
    }

    const data = JSON.parse(result.password);

    if (data && typeof data === "object" && !Array.isArray(data)) {
      return data as AppData;
    } else {
      console.warn("Deleting invalid data from Keychain:", data);
      await resetGenericPassword();
      await saveData(DEFAULT_DATA);

      return DEFAULT_DATA;
    }
  } catch (e) {
    console.error(e);
    return DEFAULT_DATA;
  }
}

async function saveData(data: AppData): Promise<void> {
  const res = await setGenericPassword(USERNAME, JSON.stringify(data));
  console.log("saved data:", res);
}

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === "dark";

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const mainStyle = {
    backgroundColor: isDarkMode ? Colors.black : Colors.white,
  };

  const [data, setData] = useState<AppData | null>(null);

  useEffect(() => {
    let mounted = true;

    loadData().then((data) => {
      if (!mounted) {
        return;
      }

      console.log("Loaded data");
      setData(data);
    });

    return () => {
      mounted = false;
    };
  }, []);

  const loaded = useRef(false);

  useEffect(() => {
    if (!data) {
      return;
    }

    if (loaded.current) {
      saveData(data).then(() => {
        console.log("Updated data in keychain:", data);
      });
    }

    loaded.current = true;
  }, [data]);

  const digitsRef = useRef<RNTextInput>(null);
  const periodRef = useRef<RNTextInput>(null);

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? "light-content" : "dark-content"}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}
        contentContainerStyle={styles.contentContainerStyle}
      >
        <Header />
        <View style={[styles.mainStyle, mainStyle]}>
          <Section title="TOTP Settings">
            <Stack vertical spacing={16}>
              <View>
                <Text>Secret Key</Text>
                <SecretInput
                  value={data?.secret ?? ""}
                  onSubmitValue={(secret) => {
                    if (secret !== data!.secret) {
                      setData((data) => ({ ...data!, secret }));
                    }
                  }}
                  placeholder="JBSWY3DPEHPK3PXP"
                />
              </View>
              <Stack horizontal spacing={32} childStyle={{ flexGrow: 1 }}>
                <View>
                  <Text>Digits</Text>
                  <TextInput
                    ref={digitsRef}
                    key={String(data?.digits)}
                    blurOnSubmit
                    defaultValue={String(data?.digits ?? DEFAULT_DATA.digits)}
                    inputMode="numeric"
                    keyboardType="number-pad"
                    maxLength={1}
                    onEndEditing={(event) => {
                      const digits = Number(event.nativeEvent.text);

                      if (!Number.isInteger(digits) || digits < 1) {
                        digitsRef.current?.setNativeProps({
                          text: String(data?.digits ?? DEFAULT_DATA.digits),
                        });
                      } else {
                        setData((data) => ({ ...data!, digits }));
                      }
                    }}
                  />
                </View>
                <View>
                  <Text>Period</Text>
                  <TextInput
                    ref={periodRef}
                    key={String(data?.period)}
                    blurOnSubmit
                    defaultValue={String(data?.period ?? DEFAULT_DATA.period)}
                    inputMode="numeric"
                    keyboardType="number-pad"
                    maxLength={3}
                    onEndEditing={(event) => {
                      const period = Number(event.nativeEvent.text);

                      if (!Number.isInteger(period) || period < 1) {
                        periodRef.current?.setNativeProps({
                          text: String(data?.period ?? DEFAULT_DATA.period),
                        });
                      } else {
                        setData((data) => ({ ...data!, period }));
                      }
                    }}
                  />
                </View>
              </Stack>
            </Stack>
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
  contentContainerStyle: { minHeight: "100%" },
  mainStyle: { flexGrow: 1 },
});

export default App;
