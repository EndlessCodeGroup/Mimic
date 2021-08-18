import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.attributes.java.TargetJvmVersion

@CacheableRule
abstract class HoconSerializationRule : ComponentMetadataRule {
    override fun execute(context: ComponentMetadataContext) {
        context.details.allVariants {
            attributes {
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
            }
        }
    }
}
