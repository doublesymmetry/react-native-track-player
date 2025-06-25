//
//  MetadataAdapter.swift
//  react-native-track-player
//
//  Created by David Chavez on 01.08.23.
//  Copyright © 2023 Double Symmetry. All rights reserved.
//

import Foundation
import AVFoundation

typealias RawMetadataGroup = [[String: Any]]

class MetadataAdapter {
    private static func getMetadataItem(metadata: [AVMetadataItem], forIdentifier: AVMetadataIdentifier) -> String? {
        return AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: forIdentifier).first?.stringValue
    }

    private static func convertToSerializableItems(items: [AVMetadataItem]) -> RawMetadataGroup {
        var rawGroup: RawMetadataGroup = []

        for metadataItem in items {
            var rawMetadataItem: [String: Any] = [:]
            rawMetadataItem["time"] = metadataItem.time.seconds
            rawMetadataItem["value"] = metadataItem.value
            rawMetadataItem["key"] = metadataItem.key
            if let commonKey = metadataItem.commonKey?.rawValue {
                rawMetadataItem["commonKey"] = commonKey
            }
            if let keySpace = metadataItem.keySpace?.rawValue {
                rawMetadataItem["keySpace"] = keySpace
            }

            rawGroup.append(rawMetadataItem)
        }

        return rawGroup
    }

    // MARK: - Public

    static func convertToGroupedMetadata(metadataGroups: [AVTimedMetadataGroup]) -> RawMetadataGroup {
        var ret: RawMetadataGroup  = []

        for metadataGroup in metadataGroups {
            let entry = convertToCommonMetadata(metadata: metadataGroup.items)
            ret.append(entry)
        }

        return ret
    }

    static func convertToCommonMetadata(metadata: [AVMetadataItem], skipRaw: Bool = false) -> [String: Any] {
        var rawMetadataItem: [String: Any] = [:]
        rawMetadataItem["title"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierTitle)
        rawMetadataItem["artist"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierArtist)
        rawMetadataItem["albumName"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierAlbumName)
        rawMetadataItem["subtitle"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataSetSubtitle)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .iTunesMetadataTrackSubTitle)
        rawMetadataItem["description"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierDescription)
        rawMetadataItem["artworkUri"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierArtwork)
        rawMetadataItem["trackNumber"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataTrackNumber)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .iTunesMetadataTrackNumber)
        rawMetadataItem["composer"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataComposer)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .iTunesMetadataComposer)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .quickTimeMetadataComposer)
        rawMetadataItem["conductor"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataConductor)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .iTunesMetadataConductor)
        rawMetadataItem["genre"] = getMetadataItem(metadata: metadata, forIdentifier: .quickTimeMetadataGenre)
        rawMetadataItem["compilation"] = getMetadataItem(metadata: metadata, forIdentifier: .iTunesMetadataDiscCompilation)
        rawMetadataItem["station"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataInternetRadioStationName)
        rawMetadataItem["mediaType"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataMediaType)
        rawMetadataItem["creationDate"] = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierCreationDate)
        rawMetadataItem["creationYear"] = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataYear)
            ?? getMetadataItem(metadata: metadata, forIdentifier: .quickTimeMetadataYear)

        if !skipRaw {
            rawMetadataItem["raw"] = convertToSerializableItems(items: metadata)
        }

        return rawMetadataItem
    }
}
