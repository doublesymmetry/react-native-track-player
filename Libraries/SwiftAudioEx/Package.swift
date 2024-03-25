// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "SwiftAudioEx",
    platforms: [.iOS(.v11)],
    products: [
        .library(
            name: "SwiftAudioEx",
            targets: ["SwiftAudioEx"]),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "SwiftAudioEx",
            dependencies: []),
        .testTarget(
            name: "SwiftAudioExTests",
            dependencies: ["SwiftAudioEx"],
            resources: [
                .process("Resources")
            ]
        ),
    ]
)
