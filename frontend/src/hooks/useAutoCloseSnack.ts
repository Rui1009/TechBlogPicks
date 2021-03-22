import {
  OptionsObject,
  SnackbarMessage,
  VariantType,
  useSnackbar
} from "notistack";

export default () => {
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();

  const snack = (message: SnackbarMessage, options?: OptionsObject) => {
    const key = enqueueSnackbar(message, options);
    setTimeout(() => closeSnackbar(key), 3000);
  };

  const snackFactory = (variant: VariantType) => (
    message: SnackbarMessage,
    options?: OptionsObject
  ) => snack(message, { variant: variant, ...options });

  const infoSnack = snackFactory("info");
  const successSnack = snackFactory("success");
  const errorSnack = snackFactory("error");
  const warningSnack = snackFactory("warning");

  return {
    snack,
    infoSnack,
    successSnack,
    errorSnack,
    warningSnack
  };
};
