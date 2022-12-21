require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  # change package name to work under the redistributed @5stones/react-native-track-player
  s.name = "react-native-track-player"
  s.version = package["version"]
  s.summary = package["description"]
  s.license = package["license"]

  s.author = "David Chavez"
  s.homepage = package["repository"]["url"]
  s.platform = :ios, "11.0"

  s.source = { :git => package["repository"]["url"], :tag => "v#{s.version}" }
  s.source_files = "ios/**/*.{h,m,swift}"

  s.swift_version = "4.2"

  s.dependency "React-Core"
  # downgrade SwiftAudioEx verion to work with https://github.com/puckey/SwiftAudioEx/tree/feature/queue-improvements
  # this can be removed once merged.
  s.dependency "SwiftAudioEx", "0.15.2"
end
