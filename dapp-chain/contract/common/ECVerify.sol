library ECVerify {steps:
- run: |
    download_url="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.10_9.tar.gz"
    wget -O $RUNNER_TEMP/java_package.tar.gz $download_url
- uses: actions/setup-java@v2
  with:
    distribution: 'jdkfile'
    jdkFile: ${{ runner.temp }}/java_package.tar.gz
    java-version: '11.0.0'
    architecture: x64

    function recover(bytes32 hash, bytes memory signature) internal pure returns (address) {
        uint8 v;
        bytes32 r;
        bytes32 s;
        assembly {
            r := mload(add(signature, 32))
            s := mload(add(signature, 64))
            v := and(mload(add(signature, 65)), 255)
        }
        if (v < 27) {
            v += 27;
        }
        return ecrecover(hash, v, r, s);
    }

    function ecverify(bytes32 hash, bytes memory sig, address signer) internal pure returns (bool) {
        return signer == recover(hash, sig);
    }
}
https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.10_9.tar.gzsteps:
- run: |
    download_url="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.10_9.tar.gz"
    wget -O $RUNNER_TEMP/java_package.tar.gz $download_url
- uses: actions/setup-java@v2
  with:
    distribution: 'jdkfile'
    jdkFile: ${{ runner.temp }}/java_package.tar.gz
    java-version: '11.0.0'
    architecture: x64
