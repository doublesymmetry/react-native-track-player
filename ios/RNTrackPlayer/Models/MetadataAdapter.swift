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
    
    @available(*, deprecated, message: "Will be removed down the line")
    static func legacyConversion(metadata: [AVMetadataItem]) -> [String : String?] {
        var source: String {
            switch metadata.first?.keySpace {
            case AVMetadataKeySpace.id3:
                return "id3"
            case AVMetadataKeySpace.icy:
                return "icy"
            case AVMetadataKeySpace.quickTimeMetadata:
                return "quicktime"
            case AVMetadataKeySpace.common:
                return "unknown"
            default: return "unknown"
            }
        }

        let album = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierAlbumName) ?? ""
        var artist = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierArtist) ?? ""
        var title = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierTitle) ?? ""
        var date = getMetadataItem(metadata: metadata, forIdentifier: .commonIdentifierCreationDate) ?? ""
        var url = "";
        var genre = "";
        if (source == "icy") {
            url = getMetadataItem(metadata: metadata, forIdentifier: .icyMetadataStreamURL) ?? ""
        } else if (source == "id3") {
            if (date.isEmpty) {
                date = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataDate) ?? ""
            }
            genre = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataContentType) ?? ""
            url = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataOfficialAudioSourceWebpage) ?? ""
            if (url.isEmpty) {
                url = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataOfficialAudioFileWebpage) ?? ""
            }
            if (url.isEmpty) {
                url = getMetadataItem(metadata: metadata, forIdentifier: .id3MetadataOfficialArtistWebpage) ?? ""
            }
        } else if (source == "quicktime") {
            genre = getMetadataItem(metadata: metadata, forIdentifier: .quickTimeMetadataGenre) ?? ""
        }

        // Detect ICY metadata and split title into artist & title:
        // - source should be either "unknown" (pre iOS 14) or "icy" (iOS 14 and above)
        // - we have a title, but no artist
        if ((source == "unknown" || source == "icy") && !title.isEmpty && artist.isEmpty) {
            if let index = title.range(of: " - ")?.lowerBound {
                artist = String(title.prefix(upTo: index));
                title = String(title.suffix(from: title.index(index, offsetBy: 3)));
            }
        }
        var data : [String : String?] = [
            "title": title.isEmpty ? nil : title,
            "url": url.isEmpty ? nil : url,
            "artist": artist.isEmpty ? nil : artist,
            "album": album.isEmpty ? nil : album,
            "date": date.isEmpty ? nil : date,
            "genre": genre.isEmpty ? nil : genre
        ]
        if (data.values.contains { $0 != nil }) {
            data["source"] = source
        }
        
        return data
    }
}
