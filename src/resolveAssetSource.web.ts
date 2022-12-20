const resolveAssetResource = (base64: any) => {
  if (/^https?:\/\//.test(base64)) {
    return base64;
  }

  // TODO: resolveAssetResource for web
  return base64;
}

export default resolveAssetResource;
