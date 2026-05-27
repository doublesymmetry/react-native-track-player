import Foundation
import React

@objc(LocalAssetHelper)
class LocalAssetHelper: NSObject {
  @objc static func requiresMainQueueSetup() -> Bool { false }

  @objc func copyToDocuments(
    _ resourceName: String,
    withExtension ext: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) {
    guard let sourceURL = Bundle.main.url(forResource: resourceName, withExtension: ext) else {
      reject("NOT_FOUND", "Bundle resource \(resourceName).\(ext) not found", nil)
      return
    }

    let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    let destURL = docs.appendingPathComponent("\(resourceName).\(ext)")

    if !FileManager.default.fileExists(atPath: destURL.path) {
      do {
        try FileManager.default.copyItem(at: sourceURL, to: destURL)
      } catch {
        reject("COPY_FAILED", error.localizedDescription, error)
        return
      }
    }

    resolve(destURL.absoluteString)
  }
}
