package ai.shreds.shared.value_objects;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedQuantityValue {

    @NotNull
    private BigDecimal value;

    @NotNull
    private String unit;

    public SharedQuantityValue add(SharedQuantityValue other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Units must match for addition");
        }
        return new SharedQuantityValue(this.value.add(other.value), this.unit);
    }

    public SharedQuantityValue subtract(SharedQuantityValue other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Units must match for subtraction");
        }
        return new SharedQuantityValue(this.value.subtract(other.value), this.unit);
    }

    public boolean isGreaterThan(SharedQuantityValue other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Units must match for comparison");
        }
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }
}