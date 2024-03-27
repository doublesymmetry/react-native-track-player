def use_rntp_local_audio!(config = nil)
    # Resolving the path the RN CLI. The `@react-native-community/cli` module may not be there for certain package managers, so we fall back to resolving it through `react-native` package, that's always present in RN projects
    cli_resolve_script = "try {console.log(require('@react-native-community/cli').bin);} catch (e) {console.log(require('react-native/cli').bin);}"
    cli_bin = Pod::Executable.execute_command("node", ["-e", cli_resolve_script], true).strip

    if (!config)
        json = []

        IO.popen(["node", cli_bin, "config"]) do |data|
          while line = data.gets
            json << line
          end
        end

        config = JSON.parse(json.join("\n"))
    end

    project_root = Pathname.new(config["project"]["ios"]["sourceDir"])
    rntpPath = Pathname.new(config["reactNativePath"]).parent.to_s + "/react-native-track-player"

    pod 'SwiftAudioEx', :path => "#{rntpPath}/Libraries/SwiftAudioEx"
end
