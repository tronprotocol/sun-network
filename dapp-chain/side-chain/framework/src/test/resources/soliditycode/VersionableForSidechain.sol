contract Versionable {

    string public initVersion = "1.0.1";//do not modify

    string public codeVersion = "1.0.4";

    event ChangeVersion(string oldVersion, string newVersion);

    function getCodeVersion() external view returns (string memory) {
        return codeVersion;
    }
}