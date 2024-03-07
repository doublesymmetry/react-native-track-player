const resolveAssetResource = (base64: unknown) => {
  if (/^https?:\/\//.test(base64 as string)) {
    return base64;
  }

  // TODO: resolveAssetResource for web
  return base64;
};

export default resolveAssetResource;
