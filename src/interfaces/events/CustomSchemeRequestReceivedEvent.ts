export interface CustomSchemeRequestReceivedEvent {
    /** UUID identifying this specific request. */
    id: string
    /** The uri, including custom scheme, for which the request is made. */
    uri: string
}
